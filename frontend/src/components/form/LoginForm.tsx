import {
    FieldGroup,
    FieldDescription,
    FieldLabel,
    Field,
    FieldError,
    FieldSeparator,
} from "@/components/ui/field";

import { useForm } from "react-hook-form";
import { InputGroup, InputGroupAddon, InputGroupButton, InputGroupInput } from "../ui/input-group";
import { Eye, EyeOff, Mail } from "lucide-react";
import { useState } from "react";
import type { LoginFormBody } from "@/types";
import { Button } from "../ui/button";
import { toast } from "../ui/toast";
import { Link, useNavigate } from "react-router-dom";
import { loginUser } from "@/lib/http-api/auth";
import axios from "axios";
import { Card, CardContent, CardHeader, CardTitle } from "../ui/card";
import { cn } from "@/lib/utils";

export function LoginForm({
    className,
    ...props
}: React.ComponentProps<"div">) {
    const {
        register,
        handleSubmit,
        clearErrors,
        setError,
        formState: { errors },
    } = useForm<LoginFormBody>({
        mode: "onSubmit",
        reValidateMode: "onSubmit",
    });

    const navigate = useNavigate();

    const [isPasswordShown, setIsPasswordShown] = useState<boolean>(false);
    const passwordInputType = isPasswordShown ? "text" : "password";
    const togglePassword = () => setIsPasswordShown((p) => !p);

    const onSubmit = async (data: LoginFormBody) => {
        try {
            await loginUser(data);
            toast.add({
                type: "success",
                description: "You have been logged in successfully.",
            });
            navigate("/");
        } catch (error) {
            if (axios.isAxiosError(error)) {
                setError("root", {
                    type: "manual",
                    message: error.response?.data?.message || "An error occurred during login.",
                });
                return;
            }

            setError("root", {
                type: "manual",
                message: "An unexpected error occurred",
            });
        }
    };

    return (
        <>
            <div className={cn("flex flex-col gap-6 px-2", className)} {...props}>
                <Card>
                    <CardHeader className="text-center">
                        <CardTitle className="text-xl">Welcome back</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <form onSubmit={handleSubmit(onSubmit)} onChange={() => clearErrors("root")}>
                            <FieldGroup>
                                <FieldSeparator className="*:data-[slot=field-separator-content]:bg-card">
                                </FieldSeparator>
                                <Field>
                                    <FieldLabel htmlFor="email">Email</FieldLabel>
                                    <InputGroup>
                                        <InputGroupAddon align={"inline-start"}>
                                            <Mail />
                                        </InputGroupAddon>
                                        <InputGroupInput
                                            type="text"
                                            id="email"
                                            placeholder="Enter your email"
                                            {...register("email", {
                                                required: "Email is required",
                                                onChange: () => clearErrors("email"),
                                            })}
                                            aria-invalid={!!errors.email}
                                            className="text-sm"
                                        />
                                    </InputGroup>
                                    {!!errors.email && <FieldError>{errors.email?.message as string}</FieldError>}
                                </Field>
                                <Field>
                                    <div className="flex items-center">
                                        <FieldLabel htmlFor="password">Password</FieldLabel>

                                        <a href="#"
                                            className="ml-auto text-sm underline-offset-4 hover:underline"
                                            tabIndex={-1}
                                        >
                                            Forgot your password?
                                        </a>
                                    </div>
                                    <InputGroup>
                                        <InputGroupInput
                                            type={passwordInputType}
                                            id="password"
                                            placeholder="Enter your password"
                                            {...register("password", {
                                                required: "Password is required",
                                                onChange: () => clearErrors("password"),
                                            })}
                                            aria-invalid={!!errors.password}
                                        />
                                        <InputGroupAddon align={"inline-end"}>
                                            <InputGroupButton
                                                type="button"
                                                tabIndex={-1}
                                                aria-label={isPasswordShown ? "hide password" : "show password"}
                                                onClick={togglePassword}
                                            >
                                                {isPasswordShown ? <Eye /> : <EyeOff />}
                                            </InputGroupButton>
                                        </InputGroupAddon>
                                    </InputGroup>
                                    {!!errors.password && <FieldError>{errors.password?.message as string}</FieldError>}
                                </Field>
                                <Field>
                                    {!!errors.root && <FieldError>{errors.root?.message as string}</FieldError>}
                                    <Button type="submit">Login</Button>
                                    <FieldDescription className="text-center">
                                        Don&apos;t have an account? <Link to="/register">Sign up</Link>
                                    </FieldDescription>
                                </Field>
                            </FieldGroup>
                        </form>
                    </CardContent>
                </Card>
                <FieldDescription className="px-6 text-center">
                    By clicking continue, you agree to our <a href="#">Terms of Service</a>{" "}
                    and <a href="#">Privacy Policy</a>.
                </FieldDescription>
            </div>
        </>
    )
}