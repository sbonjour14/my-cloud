import {
    FieldGroup,
    FieldDescription,
    FieldLabel,
    FieldLegend,
    FieldSet,
    Field,
    FieldError,
} from "@/components/ui/field";

import { useForm } from "react-hook-form";
import { InputGroup, InputGroupAddon, InputGroupButton, InputGroupInput } from "../ui/input-group";
import { Eye, EyeOff, Mail } from "lucide-react";
import { useState } from "react";
import type { LoginFormBody } from "@/types";
import { Button } from "../ui/button";
import { toast } from "../ui/toast";
import { useNavigate } from "react-router-dom";
import { loginUser } from "@/lib/http-api/auth";
import axios from "axios";

const title = "Welcome back";
const description = "Sign in to your personal cloud space.";

export function LoginForm() {
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
            const token = await loginUser(data);
            localStorage.setItem("my-cloud-token", token);
            toast.add({
                type: "success",
                description: "You have been logged in successfully.",
            });
            navigate("/");
        } catch (error) {
            console.log("CAUGHT ERROR:", error);
        }
    };

    return (
        <div className="w-full max-w-md p-4 bg-white rounded-lg shadow-md dark:bg-gray-800">
            <form onSubmit={handleSubmit(onSubmit)} onChange={() => clearErrors("root")}>
                <FieldGroup>
                    <FieldSet>
                        <FieldLegend>{title}</FieldLegend>
                        <FieldDescription>{description}</FieldDescription>
                        <p className="text-sm text-red-600 min-h-5">
                            {errors.root?.message}
                        </p>
                        <FieldGroup>
                            {/* EMAIL */}
                            <Field data-invalid={!!errors.email}>
                                <FieldLabel htmlFor="email"></FieldLabel>
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
                                        })}
                                        aria-invalid={!!errors.email}
                                        onChange={() => clearErrors("email")}
                                    />
                                </InputGroup>
                                {!!errors.email && <FieldError>{errors.email?.message as string}</FieldError>}
                            </Field>

                            {/* PASSWORD */}
                            <Field data-invalid={!!errors.password}>
                                <FieldLabel htmlFor="password"></FieldLabel>
                                <InputGroup>
                                    <InputGroupInput
                                        type={passwordInputType}
                                        id="password"
                                        placeholder="Enter your password"
                                        {...register("password", {
                                            required: "Password is required",
                                        })}
                                        aria-invalid={!!errors.password}
                                        onChange={() => clearErrors("password")}
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
                        </FieldGroup>
                    </FieldSet>
                    <Field orientation="responsive">
                        <Button type="submit">Submit</Button>
                    </Field>
                </FieldGroup>
            </form>
        </div>
    );
}